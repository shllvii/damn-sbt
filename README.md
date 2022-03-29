# DamnSBT
Repo for SBT study following "SBT Reference Manual"

## Most basics

commands: `compile`, `run`, `test`, `projects`, `inspect`

`run <argument>*`

[Configuration-level tasks](https://www.scala-sbt.org/1.x/docs/Command-Line-Reference.html#Configuration-level+tasks)

[Triggered execution](https://www.scala-sbt.org/1.x/docs/Triggered-Execution.html)

[Script mode](https://www.scala-sbt.org/1.x/docs/Scripts.html)

sbt:
```
lazy val hello = (project in file("."))
  .settings(
    name := "Hello",
    libraryDependencies += scalaTest % Test
  )
```
### Appending values
> * += will append a single element to the sequence.
> * ++= will concatenate another sequence.
#### Tasks based other keys' values
> You can compute values of some tasks or settings to define or append a value for another task. It’s done by using Def.task as an argument to :=, +=, or ++=.

```
Compile / sourceGenerators += Def.task {
  myGenerator(baseDirectory.value, (Compile / managedClasspath).value)
}
```

## Build definition

sbt DSL terminology: Setting/Task expression, Key, Setting, Task
![](https://www.scala-sbt.org/1.x/docs/files/setting-expression.png)

> On the left-hand side, name, version, and scalaVersion are keys. A key is an instance of `SettingKey[T]`, `TaskKey[T]`, or `InputKey[T]` where T is the expected value type. The kinds of key are explained below.

Keys in sbt shell: `compile`, `run`, ...
`show <task name>` show task's result
### Customized keys

In `build.sbt`,
```
lazy val helloTask = taskKey[Unit]("An example task")

lazy val helloUtil = (project in file("util"))
  .settings(
    helloTask := { println("Hello!") }
  )
```

In sbt console, type `helloUtil/helloTask`

### Task graph
`.value` method expresses dependency between tasks or settings.
> `.value` is not a normal Scala method call. `build.sbt` DSL uses a macro to lift these outside of the task body. Tasks with `.value` happens strictly **before** the task body.
`build.sbt`:
```
lazy val helloUtil = (project in file("util"))
  .settings(
    helloTask := {
      // println("Hello!")
      "Hello!"
    },
    worldTask := {
      val helloStr = helloTask.value
      println(s"$helloStr World!")
      val logger = streams.value.log
      logger.info("log: hello world~")
    }
  )
```
Run `helloUtil/worldTask`, you could get

```
Hello! World!
[info] log: hello world~
```
#### Inspecting the task
```
inspect [projectName/taskName]
```

### Task/Setting that depends on other settings
[Reference](https://www.scala-sbt.org/1.x/docs/Task-Graph.html#Defining+a+task+that+depends+on+other+settings)

## Multi-project builds

`build.sbt`
```
lazy val util = project
lazy val core = project
```
### Build-wide settings
> To factor out common settings across multiple subprojects, define the settings **scoped** to `ThisBuild`.

```
ThisBuild / version := "0.1.0"
```

### Dependencies
#### Aggregation
> Aggregation means that running a task on the aggregate project will also run it on the aggregated projects. For example,

```
lazy val hello = (project..)
  .aggregate(helloUtil, helloCore)
  .settings(
    update / aggregate := false
  )
```
`update / aggregate` is the `aggregate` key scoped to the `update` task. The setting partially inhibits the aggregation.

#### Classpath dependencies
> A project may depend on code in another project. This is done by adding a `dependsOn` method call. This mechanism creates an ordering among projects, and allows code in one project to use classes from others as well.

```
lazy val hello = (project..)
  .dependsOn(helloUtil % "compile->helloTask")
```
[Per-configuration classpath dependencies](https://www.scala-sbt.org/1.x/docs/Multi-Project.html#Per-configuration+classpath+dependencies)





### Cross-building

[Reference](https://www.scala-sbt.org/1.x/docs/Cross-Build.html)

## Scope
> There is no single value for a given key name, because the value may differ according to scope.

A scope axis is a type constructor similar to `Option[A]`, that is used to form a component in a scope. There are three scope axes:
* The subproject axis
* The dependency configuration axis

### Configuration axis
> A dependency configuration (or “configuration” for short) defines a graph of library dependencies, potentially with its own classpath, sources, generated packages, etc. 

![Configuration relationship](https://www.scala-sbt.org/1.x/docs/files/sbt-configurations.png)

### Zero scope
> Each scope axis can be filled in with an instance of the axis type (analogous to `Some(_)`), or the axis can be filled in with the special value Zero. So we can think of Zero as `None`.

```
lazy val root = (project in file("."))
  .settings(
    name := "hello"
  )
```
The code above sets `name` in scope `root/Zero/Zero`.

`Global` implies `Zero / Zero /Zero`.

### When to specify a scope 

> You need to specify the scope if the key in question is normally scoped. For example, the `compile` task, by default, is scoped to `Compile` and `Test` configurations, and does not exist outside of those scopes.

### Scope delegation

> A scoped key may be undefined, if it has no value associated with it in its scope.

> For each scope axis, sbt has a fallback search path made up of other scope values. Typically, if a key has no associated value in a more-specific scope, sbt will try to get a value from a more general scope, such as the `ThisBuild` scope.

Rules:
* Rule 1: Scope axes have the following precedence: the subproject axis, the configuration axis, and then the task axis.
* Rule 2: Given a scope, delegate scopes are searched by substituting the task axis in the following order: the given task scoping, and then Zero, which is non-task scoped version of the scope.
* Rule 3: Given a scope, delegate scopes are searched by substituting the configuration axis in the following order: the given configuration, its parents, their parents and so on, and then Zero (same as unscoped configuration axis).
* Rule 4: Given a scope, delegate scopes are searched by substituting the subproject axis in the following order: the given subproject, ThisBuild, and then Zero.
* Rule 5: A delegated scoped key and its dependent settings/tasks are evaluated without carrying the original context.

### Summary
* A scope is a tuple of components in three axes: the subproject axis, the configuration axis, and the task axis.
* There’s a special scope component Zero for any of the scope axes.
* There’s a special scope component ThisBuild for the subprojects axis only.
* Test extends Runtime, and Runtime extends Compile configuration.
* A key placed in build.sbt is scoped to ${current subproject} / Zero / Zero by default.
* A key can be scoped using / operator.


## Library dependencies

```
libraryDependencies += groupID % artifactID % revision
libraryDependencies += groupID % artifactID % revision % configuration
libraryDependencies ++= Seq(
  groupID % artifactID % revision,
  groupID % otherID % otherRevision
)
```

## More reference

[Detailed configuration](https://www.scala-sbt.org/1.x/docs/Configuring-Scala.html)
[Macro project](https://www.scala-sbt.org/1.x/docs/Macro-Projects.html)
[Detailed tasks](https://www.scala-sbt.org/1.x/docs/Tasks.html)
[Plugins](https://www.scala-sbt.org/1.x/docs/Best-Practices.html)