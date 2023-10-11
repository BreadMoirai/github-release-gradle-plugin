package com.github.breadmoirai.githubreleaseplugin.ast

class ExtensionPropertyException extends RuntimeException {
    ExtensionPropertyException() {
        super()
    }

    ExtensionPropertyException(String message) {
        super(message)
    }

    ExtensionPropertyException(String message, Throwable cause) {
        super(message, cause)
    }

    ExtensionPropertyException(Throwable cause) {
        super(cause)
    }

    protected ExtensionPropertyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace)
    }
}
