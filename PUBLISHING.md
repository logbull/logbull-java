# Publishing to Maven Central

This document describes how to publish LogBull Java libraries to Maven Central.

## Prerequisites

1. **Central Portal Account**: Register at https://central.sonatype.com/
   - Follow the [registration documentation](https://central.sonatype.org/register/central-portal/)
   - Verify your namespace ownership (typically via DNS TXT record or GitHub repository)
   - Note: The legacy OSSRH service at issues.sonatype.org has been decommissioned
2. **GPG Key**: Generate a GPG key pair for signing artifacts
3. **GitHub Secrets**: Configure the following secrets in your GitHub repository:
   - `OSSRH_USERNAME`: Your Central Portal username
   - `OSSRH_PASSWORD`: Your Central Portal password or token
   - `SIGNING_KEY_ID`: Your GPG key ID (last 8 characters)
   - `SIGNING_KEY`: Your GPG private key (base64 encoded)
   - `SIGNING_PASSWORD`: Your GPG key passphrase

## Versioning Strategy

This project uses semantic versioning with automatic version bumping based on commit messages:

- **FEATURE**: Commits starting with `FEATURE` trigger a **minor** version bump (e.g., 1.0.0 → 1.1.0)
- **FIX**: Commits starting with `FIX` trigger a **patch** version bump (e.g., 1.0.0 → 1.0.1)
- **Other**: Other commits (REFACTOR, etc.) don't trigger automatic releases

### Example Commit messages

```bash
git commit -m "FEATURE Add Spring Boot starter auto-configuration"
git commit -m "FIX Resolve memory leak in log sender"
git commit -m "REFACTOR Improve code organization"
```

## Automated Publishing

### Automatic Release on Push to Main

When you push to `main` or `master` branch:

1. The `release.yml` workflow runs
2. It analyzes commits since the last tag
3. If FEATURE or FIX commits are found, it creates a new version tag
4. The tag push triggers the `publish.yml` workflow
5. Artifacts are built, signed, and published to Maven Central

### Manual Release

To manually trigger a release:

```bash
# Create and push a version tag
git tag v1.0.1
git push origin v1.0.1
```

This will trigger the `publish.yml` workflow.

## Local Publishing (Testing)

To test publishing locally without uploading to Maven Central:

```bash
# Publish to local Maven repository
./gradlew publishToMavenLocal

# Check artifacts in ~/.m2/repository/com/logbull/
```

## GPG Key Setup

### Generate GPG Key

```bash
# Generate key
gpg --gen-key

# List keys
gpg --list-keys

# Export public key to keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export private key (base64 encoded for GitHub secret)
gpg --export-secret-keys YOUR_KEY_ID | base64 > private-key.txt
```

### Configure GitHub Secrets

1. Go to your repository Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `SIGNING_KEY_ID`: Last 8 characters of your GPG key ID
   - `SIGNING_KEY`: Contents of `private-key.txt`
   - `SIGNING_PASSWORD`: Your GPG key passphrase

## Published Artifacts

The following artifacts are published:

1. **logbull** - Core library

   - `logbull-{version}.jar`
   - `logbull-{version}-sources.jar`
   - `logbull-{version}-javadoc.jar`

2. **logbull-spring-boot-starter** - Spring Boot starter
   - `logbull-spring-boot-starter-{version}.jar`
   - `logbull-spring-boot-starter-{version}-sources.jar`
   - `logbull-spring-boot-starter-{version}-javadoc.jar`

## Troubleshooting

### Signing Fails

- Verify GPG key is correctly base64 encoded
- Check that `SIGNING_PASSWORD` matches your GPG key passphrase
- Ensure key hasn't expired: `gpg --list-keys`

### Upload to Maven Central Fails

- Verify credentials are correct
- Check that your Central Portal account has permissions for `com.logbull` namespace
- Ensure artifacts meet Maven Central requirements (sources, javadoc, POM metadata)
- For namespace issues, contact [Central Support](mailto:central-support@sonatype.com)

### Version Already Exists

- Maven Central doesn't allow overwriting published versions
- Bump the version and try again
- Delete the tag if needed: `git tag -d v1.0.0 && git push origin :refs/tags/v1.0.0`

## References

- [Central Portal Publishing Guide](https://central.sonatype.org/publish/publish-guide/)
- [Central Portal Registration](https://central.sonatype.org/register/central-portal/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
- [GPG Signing Guide](https://central.sonatype.org/publish/requirements/gpg/)
- [Central Support](mailto:central-support@sonatype.com)