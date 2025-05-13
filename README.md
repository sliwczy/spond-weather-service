# Spond Weather Service
A simple weather service utilizing met.no Weather API 

here is a design for the architecture and data model of the project as well as some notes and cosiderations: 
<br> https://www.figma.com/board/Ou6dpByqPDVUvqqgnKHO3L/Welcome-to-FigJam?node-id=0-1&t=ngsFpezkl75NBcVe-1
<br> (let me know if above link does not work for you)

did not get enough time to create Dockerfiles to spin RabbitMQ and Weather service

but to run service :
1) Ensure docker is installed and available in the command line
2) run `docker run -it --rm --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:4.0-management`
3) run `java -jar WeatherService.jar --spring.profiles.active=prod`