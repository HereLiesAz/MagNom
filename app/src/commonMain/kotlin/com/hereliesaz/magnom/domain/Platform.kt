package com.hereliesaz.magnom.domain

/** A fresh unique identifier for a new card. Implemented per platform (UUID on both JVM targets). */
expect fun newId(): String
