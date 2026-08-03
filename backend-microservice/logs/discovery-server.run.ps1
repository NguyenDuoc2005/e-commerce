Set-Location "C:\My Project\e-commerce"
$env:EUREKA_DEFAULT_ZONE="http://localhost:8761/eureka"
& "C:\My Project\e-commerce\BE\gradlew.bat" -p "C:\My Project\e-commerce\backend-microservice" :discovery-server:bootRun *> "C:\My Project\e-commerce\backend-microservice\logs\discovery-server.out.log"
