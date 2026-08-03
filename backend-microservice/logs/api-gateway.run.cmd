@echo off
cd /d "C:\My Project\e-commerce"
set "EUREKA_DEFAULT_ZONE=http://localhost:8761/eureka"
call "C:\My Project\e-commerce\BE\gradlew.bat" -p "C:\My Project\e-commerce\backend-microservice" :api-gateway:bootRun > "C:\My Project\e-commerce\backend-microservice\logs\api-gateway.out.log" 2>&1
