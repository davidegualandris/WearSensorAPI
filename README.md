### How to start the API ###

- Build the image -> docker image build -t wearsensorapi_image .
- Publish the service on port 80 -> docker container run -it --publish 80:8080 wearsensorapi_image
