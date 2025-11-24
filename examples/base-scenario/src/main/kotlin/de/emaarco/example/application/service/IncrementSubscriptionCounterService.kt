package de.emaarco.example.application.service

import de.emaarco.example.application.port.`in`.IncrementSubscriptionCounterUseCase
import de.emaarco.example.application.port.out.SubscriptionCounterRepository
import de.emaarco.example.domain.SubscriptionId
import jakarta.transaction.Transactional
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
@Transactional
class IncrementSubscriptionCounterService(
    private val counterRepository: SubscriptionCounterRepository
) : IncrementSubscriptionCounterUseCase {

    private val log = KotlinLogging.logger {}

    override fun incrementCounter(subscriptionId: SubscriptionId) {
        val counter = counterRepository.find()
        val updatedCounter = counter.increment()
        counterRepository.save(updatedCounter)
        log.info { "Incremented subscription counter for ${subscriptionId.value}: ${updatedCounter.count}" }
    }
}
