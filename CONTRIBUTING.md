# Contributing to SLAB


## Getting started

There are two approaches to start the development cycle

  * Use [loop](https://github.com/criteo/loop):
    
    Simply execute `npm run start`, it will build and start the server of the `example` project
      
  * Alternatively, you can manually lauch the webapp and the `example` project's server:
    
    - Example project

      This project is created to facilitate the development project, it's located in the `example` folder, 

      - Compile the project

          `sbt example/compile`

      - Launch the main class

          `com.criteo.slab.example.Launcher`


    - Web application

      `src/main/webapp` contains all web application code and resources.

      - Install npm packages

        `npm install`

      - Start web dev server

        `npm run serve -- --env.serverPort=$SERVER_PORT`

        where `SERVER_PORT` should be the port of SLAB web server instance
      
## Utilities

  - Generate a package for all supported Scala versions
  
    `sbt +package`

  - Test Scala code
  
    `sbt test`
    
  - Test JavaScript code
  
    `npm run test`
