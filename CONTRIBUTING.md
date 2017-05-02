# Contributing to Slab


## Modules
  - Example

    We created an example project to facilitate the development process located in the `example` folder, 
    
    - Compile the project
    
        `sbt example/compile`
        
    - Launch the main class
    
        `com.criteo.slab.example.Launcher`
    

  - Web application

    `src/main/webapp` contains all web application code and resources.

    - Install npm packages

      `npm install`

    - Start web dev server

      `npm run serve -- --env.port=$SERVER_PORT`

      where `SERVER_PORT` should be the port of Slab web server instance
      
## Development

  - Test Scala code
  
    `sbt test`
    
  - Test JavaScript code
  
    `npm run test`
