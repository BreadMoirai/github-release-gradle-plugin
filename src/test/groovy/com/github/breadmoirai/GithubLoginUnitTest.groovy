/*
 *    Copyright 2017 - 2018 BreadMoirai (Ton Ly)
 *
 *    Licensed under the Apache License, Version 2.0 (the 'License');
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an 'AS IS' BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.breadmoirai

import javafx.application.Platform
import javafx.scene.input.KeyCode
import javafx.stage.Stage
import org.testfx.framework.spock.ApplicationSpec
import spock.lang.Shared
import spock.lang.Unroll

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Unroll
class GithubLoginUnitTest extends ApplicationSpec {

    @Shared
    GithubLoginApp app
    private Stage stage

    @Override
    void start(Stage stage) throws Exception {
        app = new GithubLoginApp()
        this.stage = stage
        app.start(this.stage)
    }

    def cleanup() {
        Platform.runLater{ stage.close() }
    }

    def 'Keyboard Login Test'(String username, String password, String encoded) {

        when: 'Username and Password is entered'
        write(username)
        press(KeyCode.TAB)
        release(KeyCode.TAB)
        write(password)
        press(KeyCode.ENTER)
        release(KeyCode.ENTER)

        then: 'Expect correct credentials'
        Optional<String> result = app.future.get(1, TimeUnit.MICROSECONDS)
        result.isPresent()
        !result.get().isEmpty()
        result.get() == encoded

        where:
        username | password            | encoded
        'user'   | 'pass'              | 'dXNlcjpwYXNz'
        'whoami' | '@S3K\u03A9\u00AEE' | 'd2hvYW1pOkBTM0vOqcKuRQ=='
    }

    def 'Mouse Login Test'(String username, String password, String encoded) {

        when: 'Username and Password is entered'
        clickOn("#field-username")
        write(username)
        clickOn("#field-password")
        write(password)
        clickOn("#button-login")

        then: 'Expect correct credentials'
        Optional<String> result = app.future.get(1, TimeUnit.MICROSECONDS)
        result.isPresent()
        !result.get().isEmpty()
        result.get() == encoded

        where:
        username | password            | encoded
        'user'   | 'pass'              | 'dXNlcjpwYXNz'
        'whoami' | '@S3K\u03A9\u00AEE' | 'd2hvYW1pOkBTM0vOqcKuRQ=='
    }

    def 'Keyboard Failure Test'(String username, String password) {

        given: "Only 1 of Username and Password is entered"
        write(username)
        press(KeyCode.TAB)
        release(KeyCode.TAB)
        write(password)
        press(KeyCode.ENTER)
        release(KeyCode.ENTER)

        when: "Input is queried immediately"
        app.future.get(1, TimeUnit.MICROSECONDS)

        then: "A Timeout Exception is thrown"
        thrown TimeoutException

        where:
        username | password
        'user'   | ''
        ''       | 'pass'
    }

    def 'Click Failure Test'() {

        given: "Only 1 of Username and Password is entered"
        clickOn("#field-username")
        write(username)
        clickOn("#field-password")
        write(password)
        clickOn("#button-login")

        when: "Input is queried immediately"
        app.future.get(1, TimeUnit.MICROSECONDS)

        then: "A Timeout Exeption is thrown"
        thrown TimeoutException

        where:
        username | password
        'user'   | ''
        ''       | 'pass'
    }



    def 'Keyboard Cancel Test'() {
        when: 'ESC key is pressed'
        press(KeyCode.ESCAPE)
        release(KeyCode.ESCAPE)

        then: 'Result is empty'
        !app.future.get(1, TimeUnit.MICROSECONDS).isPresent()
    }
    
    def 'Mouse Cancel Test'() {
        when: 'Cancel button is clicked'
        clickOn("#button-cancel")

        then: 'Result is empty'
        !app.future.get(1, TimeUnit.MICROSECONDS).isPresent()
    }


}
