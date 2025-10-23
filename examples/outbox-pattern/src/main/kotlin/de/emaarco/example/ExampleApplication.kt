package de.emaarco.example

import io.camunda.client.annotation.Deployment
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableJpaRepositories
@EnableScheduling
@Deployment(resources = ["classpath:/newsletter.bpmn"])
class ExampleApplication

fun main(args: Array<String>) {
    runApplication<ExampleApplication>(*args)
}
