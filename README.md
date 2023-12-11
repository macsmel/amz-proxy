To start you need to fill sockets.txt and run command: 

**spring-boot:run**


For remote debugging:

**spring-boot:run "-Dspring-boot.run.jvmArguments=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"**

**Test requests:**

**GET**

curl --location 'localhost:8080/v1/proxy?url=https%3A%2F%2Fwww.amazon.com%2FReversible-Breathable-Tropical-Eco-Friendly-78-74x59-06in%2Fdp%2FB0CFVKQYDW%2Fref%3Dsr_1_2%3Fkeywords%3DOutdoor%2BPlastic%2BStraw%2BRug%26qid%3D1702056389%26sr%3D8-2'

**POST**

curl --location 'localhost:8080/v1/proxy?url=https%3A%2F%2Freqres.in%2Fapi%2Fusers&encoding=application%2Fjson' \
--header 'Content-Type: text/plain' \
--data '{\"name\": \"morpheus\", \"job\": \"leader\"}'