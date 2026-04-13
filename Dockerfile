FROM ubuntu:latest
LABEL authors="vipin"

ENTRYPOINT ["top", "-b"]