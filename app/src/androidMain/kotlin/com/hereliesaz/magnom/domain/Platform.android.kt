package com.hereliesaz.magnom.domain

import java.util.UUID

actual fun newId(): String = UUID.randomUUID().toString()
