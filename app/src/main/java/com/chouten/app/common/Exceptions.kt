package com.chouten.app.common

class ModuleFolderNotFoundException : Exception() {
    override val message: String
        get() = "The module folder could not be found"
}

class OutOfDateModuleException(override val message: String) : Exception()
class OutOfDateAppException(override val message: String) : Exception()