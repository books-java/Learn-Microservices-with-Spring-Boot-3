cd multiplication  
./mvnw clean package -D maven.test.skip=true

cd ../gamification
./mvnw clean package -D maven.test.skip=true

cd ../gateway
./mvnw clean package -D maven.test.skip=true