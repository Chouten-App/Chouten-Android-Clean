package com.chouten.app.domain.model

/**
 * Represents a semantic version number.
 *
 * @property major The major version number, used to indicate incompatible API changes.
 * @property minor The minor version number, used to indicate new, but backwards compatible, functionality.
 * @property patch The patch version number, used to indicate backwards compatible bug fixes and small changes.
 * @property preRelease The pre-release identifier.
 * @property buildMetadata The build metadata.
 */
data class Version(
    var major: Int,
    var minor: Int,
    var patch: Int,
    var preRelease: String = "",
    var buildMetadata: String = ""
) : Comparable<Version> {

    /**
     * Creates a [Version] object from a version string.
     *
     * @param versionString The version string to parse.
     * @param useRegex Flag to determine whether to use regex for parsing.
     *          Not using regex is stricter and will throw an exception for more invalid strings.
     * @throws IllegalArgumentException If the version string is not valid.
     */
    constructor(versionString: String, useRegex: Boolean = false) : this(
        major = 0, minor = 0, patch = 0
    ) {
        val parsedVersion = parse(versionString, useRegex)
        major = parsedVersion.major
        minor = parsedVersion.minor
        patch = parsedVersion.patch
        preRelease = parsedVersion.preRelease
        buildMetadata = parsedVersion.buildMetadata
    }

    companion object {
        private val SEMVER_REGEX = Regex(
            "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?"
        )

        /**
         * Parses a version string into a [Version] object.
         *
         * @param versionString The version string to parse.
         * @param useRegex Flag to determine whether to use regex for parsing.
         *          Not using regex is stricter and will throw an exception for more invalid strings.
         * @return The parsed [Version] object.
         * @throws IllegalArgumentException If the version string is not valid.
         */
        fun parse(versionString: String, useRegex: Boolean = false): Version {
            if (useRegex) {
                val matchResult = SEMVER_REGEX.matchEntire(versionString)
                    ?: throw IllegalArgumentException("Invalid semantic version format")

                return Version(
                    major = matchResult.groupValues[1].toInt(),
                    minor = matchResult.groupValues[2].toInt(),
                    patch = matchResult.groupValues[3].toInt(),
                    preRelease = matchResult.groupValues[4],
                    buildMetadata = matchResult.groupValues[5]
                )
            } else {
                val metadataSplit = versionString.split("+", limit = 2)
                val mainPart = metadataSplit[0]
                val buildMetadata = if (metadataSplit.size > 1) metadataSplit[1] else ""

                val preReleaseSplit = mainPart.split("-", limit = 2)
                val numbers = preReleaseSplit[0].split(".")
                val preRelease = if (preReleaseSplit.size > 1) preReleaseSplit[1] else ""

                if (numbers.size != 3) {
                    throw IllegalArgumentException("Invalid version format. Expected format: MAJOR.MINOR.PATCH")
                }

                val major = numbers[0].toIntOrNull()
                    ?: throw IllegalArgumentException("Major version is not a valid integer")
                val minor = numbers[1].toIntOrNull()
                    ?: throw IllegalArgumentException("Minor version is not a valid integer")
                val patch = numbers[2].toIntOrNull()
                    ?: throw IllegalArgumentException("Patch version is not a valid integer")

                return Version(major, minor, patch, preRelease, buildMetadata)
            }
        }
    }

    /**
     * Compares this version with the specified version for order.
     *
     * @param other The [Version] to be compared.
     * @return A negative integer if this version is less than the other version,
     * zero if they are equal or a positive integer if this version is greater than the other version.
     */
    override fun compareTo(other: Version): Int {
        if (this.major != other.major) return this.major - other.major
        if (this.minor != other.minor) return this.minor - other.minor
        if (this.patch != other.patch) return this.patch - other.patch
        if (this.preRelease != other.preRelease) {
            if (this.preRelease.isEmpty()) return 1
            if (other.preRelease.isEmpty()) return -1
            // Split preRelease strings and compare them part by part.
            val thisPreReleaseParts = this.preRelease.split(".")
            val otherPreReleaseParts = other.preRelease.split(".")
            val maxIndex = minOf(thisPreReleaseParts.size, otherPreReleaseParts.size)
            for (i in 0 until maxIndex) {
                val cmp = thisPreReleaseParts[i].compareTo(otherPreReleaseParts[i])
                if (cmp != 0) return cmp
            }
            return thisPreReleaseParts.size - otherPreReleaseParts.size
        }
        // Note: Build metadata does not affect version precedence
        return 0
    }

    /**
     * Checks if this version is equal to the specified version.
     *
     * @param other The [Version] to compare.
     * @return `true` if the versions are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Version

        if (major != other.major) return false
        if (minor != other.minor) return false
        if (patch != other.patch) return false
        if (preRelease != other.preRelease) return false

        return true
    }

    /**
     * @return a hash code value for the version.
     */
    override fun hashCode(): Int {
        var result = major
        result = 31 * result + minor
        result = 31 * result + patch
        result = 31 * result + preRelease.hashCode()
        result = 31 * result + buildMetadata.hashCode()
        return result
    }

    /**
     * Returns a string representation of the version.
     */
    override fun toString(): String {
        return "$major.$minor.$patch" +
                (if (preRelease.isNotEmpty()) "-$preRelease" else "") +
                (if (buildMetadata.isNotEmpty()) "+$buildMetadata" else "")
    }
}
