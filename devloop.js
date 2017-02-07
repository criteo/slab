/* eslint-disable */
let config = loadJson('dev.config.json')

let installDependencies = run({
  name: 'npm',
  cwd: '.',
  sh: 'npm install',
  watch: 'package.json'
})

let flow = run({
  name: 'flow',
  cwd: '.',
  sh: 'node ./node_modules/flow-bin/cli.js',
  watch: ['src/**/*.js', 'src/**/*.jsx']
}).dependsOn(installDependencies)

let webpack = run({
  name: 'webpack',
  cwd: '.',
  sh: './node_modules/.bin/webpack --bail --env.out=target/scala-2.11/classes/public',
  watch: 'webpack.config.js'
}).dependsOn(flow, installDependencies)

let sbt = startSbt({
  sh: 'sbt',
  watch: ['build.sbt']
})

let packageDependencies = sbt.run({
  name: 'server deps',
  command: 'assemblyPackageDependency'
})

let compileServer = sbt.run({
  name: 'scalac',
  command: 'example/compile',
  watch: ['src/**/*.scala']
}).dependsOn(packageDependencies)

let separator = platform == 'win32' ? ';': ':'
let version = '0.1.0-SNAPSHOT'
let server = runServer({
  name: 'server',
  httpPort,
  env: config.env,
  sh: `java -cp "target/scala-2.11/slab-assembly-${version}-deps.jar${separator}target/scala-2.11/classes${separator}${config.extraClasspath || ''}" ${config.mainClass} -http.port=:${httpPort} -doc.root=public`
}).dependsOn(compileServer)

proxy(server, 8080).dependsOn(webpack)