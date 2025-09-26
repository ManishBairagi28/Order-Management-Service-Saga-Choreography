package com.orderms.order.repository;

import com.orderms.order.entity.SagaInstance;
import com.orderms.shared.saga.SagaState;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SagaRepository extends ReactiveMongoRepository<SagaInstance, String> {

    Mono<SagaInstance> findByOrderId(String orderId);

    Flux<SagaInstance> findByCurrentState(SagaState state);

    Flux<SagaInstance> findByCurrentStateIn(SagaState... states);
}