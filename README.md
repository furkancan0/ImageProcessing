# ImageProcessing
This is an Image Processing System built with Java Spring Boot, featuring user authentication and various image processing functionalities. Image data stored in a PostgreSQL database. The project utilizes Java's  Imgscalr library for image transformations.

Image Processing  
- Transform Image   
- Resize  
- Crop  
- Rotate    
- Flip  
- Mirror  
- Compress  
- Change format (JPEG, PNG, etc.)  
- Grayscale filter
- Search by filter
- Paginated images

Used Technologies  
- Spring (Boot, Data, Security)
- JPA / Hibernate
- PostgreSQL
- RabbitMQ
- Test containers
- Junit, Mockito
- Deployment Docker, github pipelines
- Lombok
- Prometheus
- Grafana

Example Workflow Summary  
After image upload:  
- Save image in DB
- Publish event image.uploaded to RabbitMQ  
- Consumers listen and perform:  
--> Thumbnail creation  
--> Compression  
--> Metadata extraction  
--> Search index update  
--> Notification  
--> Analytics 

# 🛠️Includes all service and integration tests.   

# Project Idea  
- https://roadmap.sh/projects/image-processing-service


