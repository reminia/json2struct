addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")

resolvers += Resolver.url("GitHub Package Registry", url("https://maven.pkg.github.com/reminia/_"))(
  Resolver.ivyStylePatterns
)
credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  System.getenv("GITHUB_REPOSITORY_OWNER"),
  System.getenv("GITHUB_TOKEN")
)
addSbtPlugin("me.yceel" % "sbt-curl" % "0.1.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")
