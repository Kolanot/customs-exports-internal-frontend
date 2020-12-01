import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.3.0-play-26",
    "uk.gov.hmrc"          %% "logback-json-logger"           % "4.8.0",
    "uk.gov.hmrc"          %% "govuk-template"                % "5.55.0-play-26",
    "uk.gov.hmrc"          %% "play-health"                   % "3.15.0-play-26",
    "uk.gov.hmrc"          %% "play-ui"                       % "8.11.0-play-26",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-26"    % "2.24.0",
    "uk.gov.hmrc"          %% "play-frontend-govuk"           % "0.49.0-play-26",
    "org.webjars.npm"      %  "govuk-frontend"                % "3.8.1",
    "uk.gov.hmrc"          %% "play-json-union-formatter"     % "1.10.0-play-26",
    "uk.gov.hmrc"          %% "simple-reactivemongo"          % "7.30.0-play-26",
    "ai.x"                 %% "play-json-extensions"          % "0.42.0",
    "com.github.tototoshi" %% "scala-csv"                     % "1.3.6",
    "com.github.cloudyrock.mongock"  %  "mongock-core"        % "2.0.2",
    "org.mongodb"          %  "mongo-java-driver"             % "3.12.1",
    "org.webjars.npm"      %  "hmrc-frontend"                 % "1.5.0",
    "org.webjars.npm"      %  "accessible-autocomplete"       % "2.0.3"
  ).map(_.withSources())

  val test = Seq(
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.13.1"                % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.mockito"             %  "mockito-core"             % "3.5.7"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.27.1"                % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.3"                 % "test, it",
    "uk.gov.hmrc"             %% "reactivemongo-test"       % "4.16.0-play-26"        % "test, it"
  ).map(_.withSources())

}
