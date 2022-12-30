# cqrs-kafka
CQRS with Kafka

## Docker
 
### Create Docker Network - techbankNet 
```
  docker network create --attachable -d bridge techbankNet
```

### Docker compose
```
  cd docker
  docker-compose -f kafka-compose.yaml up -d
  docker-compose -f kafka-compose.yaml down
```


## SpringBoot Projects

 - Main project under ./projects/bank-account
