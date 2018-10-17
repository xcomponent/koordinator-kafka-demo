[![](http://slack.xcomponent.com/badge.svg)](http://slack.xcomponent.com/)
[![CircleCI](https://circleci.com/gh/xcomponent/koordinator-kafka-demo/tree/master.svg?style=svg)](https://circleci.com/gh/xcomponent/koordinator-kafka-demo/tree/master)
[![TypeScript](https://badges.frapsoft.com/typescript/love/typescript.png?v=101)](https://github.com/ellerbrock/typescript-badges/)

# Koordinator Kafka Demo

This repository demos the use of Koordinator to integrate batch and stream
based jobs. In this example, we have a batch job `image-search`, that given some
search terms generate a list of urls of images matching the terms; and
`download-image`, a Kafka producer/consumer written in Java that allows one to
download the images in parallel.

## Requirements

* A Kafka broker, in our tests we use [this](https://hub.docker.com/r/spotify/kafka/) docker container
* Set the variables `WORKER_USERNAME` and `WORKER_PASSWORD` as your credentials
  in the Koordinator server.

## Build

1. Install the dependencies of the `image-search` worker: `cd image-search && yarn install && cd ..`
2. Build the `download-image` worker: `cd download-image && ./build.sh && cd ..`

## Run

Just run the script `./test.sh`. It will start the workers and the workflow.
You should be able to monitor its execution in the Koordinator frontend.

### Submit a Pull Request

1.  Fork it!
2.  Create your feature branch: `git checkout -b my-new-feature`
3.  Commit your changes: `git commit -am 'Add some feature'`
4.  Push to the branch: `git push origin my-new-feature`
5.  Submit a pull request

## License

[Apache License V2](https://raw.githubusercontent.com/xcomponent/koordinator-kafka-demo/master/LICENSE)

