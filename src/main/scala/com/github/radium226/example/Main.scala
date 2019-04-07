package com.github.radium226.example

import java.nio.file.Paths

import akka._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import cats.implicits._
import cats.effect._
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits._

object Main extends IOApp {

  def source(range: Range): Source[Int, NotUsed] = {
    Source(range)
  }

  def sink: Sink[Int, Future[Done]] = {
    Sink.foreach[Int]({ number =>
      println(number)
    })
  }

  def actorSystem[F[_]](config: Config)(implicit F: Async[F]): Resource[F, ActorSystem] = {
    def acquire: F[ActorSystem] = {
      F.delay {
        ActorSystem("system", config)
      }
    }

    def release(actorSystem: ActorSystem): F[Unit] = {
      F.async { callback =>
        actorSystem
          .terminate()
          .onComplete({ result =>
            callback(result.toEither.map({ _ => () }))
          })
      }
    }

    Resource.make(acquire)(release)
  }

  def actorMaterializer[F[_]](actorSystem: ActorSystem)(implicit F: Sync[F]): Resource[F, ActorMaterializer] = {
    def acquire: F[ActorMaterializer] = {
      F.delay {
        ActorMaterializer()(actorSystem)
      }
    }

    def release(actorMaterializer: ActorMaterializer): F[Unit] = {
      F.delay {
        actorMaterializer.shutdown()
      }
    }

    Resource.make(acquire)(release)
  }

  def run(arguments: List[String]): IO[ExitCode] = {
    //val config = ConfigFactory.parseFile(Paths.get("./reference.conf").toFile).resolve()

    //println(config.root().render())

    val config = ConfigFactory.load()

    val resources = for {
      actorSystem <- actorSystem[IO](config)
      actorMaterializer <- actorMaterializer[IO](actorSystem)
    } yield (actorSystem, actorMaterializer)

    resources
      .use({ case (actorSystem, actorMaterializer) =>
        val graph = source(0 to 10).toMat(sink)(Keep.right)
        IO.fromFuture(IO(graph.run()(actorMaterializer)))
      })
      .as(ExitCode.Success)
  }

}
