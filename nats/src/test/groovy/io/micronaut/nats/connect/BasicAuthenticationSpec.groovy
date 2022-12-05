/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.nats.connect


import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.nats.annotation.NatsConnection
import io.nats.client.Connection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Shared
import spock.lang.Specification

class BasicAuthenticationSpec extends Specification {

    @Shared
    GenericContainer usernamePasswordContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .withCommand("--user", "test", "--pass", "test")
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    @Shared
    GenericContainer authContainer =
            new GenericContainer("nats:latest")
                    .withExposedPorts(4222)
                    .withCommand("--auth", "randomToken")
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server is ready.*"))

    void "username and passwort authentication"() {
        given:
        usernamePasswordContainer.start()
        String address = "nats://localhost:${usernamePasswordContainer.getMappedPort(4222)}"
        ApplicationContext context = ApplicationContext.run(
                ["spec.name"   : getClass().simpleName,
                 "nats.default.addresses": [address],
                 "nats.default.username": "test",
                 "nats.default.password": "test"])

        expect:
        context.getBean(Connection, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION)).getStatus() == Connection.Status.CONNECTED

        cleanup:
        context.stop()
        usernamePasswordContainer.stop()

    }

    void "token authentication"() {
        given:
        authContainer.start()
        String address = "nats://localhost:${authContainer.getMappedPort(4222)}"
        ApplicationContext context = ApplicationContext.run(
                ["spec.name"   : getClass().simpleName,
                 "nats.default.addresses": [address],
                 "nats.default.token": "randomToken"])

        expect:
        context.getBean(Connection, Qualifiers.byName(NatsConnection.DEFAULT_CONNECTION)).getStatus() == Connection.Status.CONNECTED

        cleanup:
        context.stop()
        authContainer.stop()

    }
}
