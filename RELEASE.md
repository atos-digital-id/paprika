
# How to release

## Tag release commits

Check what should be released:
```bash
mvn paprika:release
```
Tag commits:
```bash
mvn paprika:release exec=true
```
Push tags:
```bash
git push --tags
```

## Build and deploy

[Trigger CI workflow on main branch.](https://github.com/atos-digital-id/paprika/actions/workflows/maven.yml)

## Release to Maven Central

[Connect to OSSRH](https://s01.oss.sonatype.org/):

  * [Go to Staging Repositories](https://s01.oss.sonatype.org/#stagingRepositories)
  * Select the staging repository.
  * Close it and release it.

## Update documentation site

Run:
```bash
checkout docs
rebase --strategy-option=theirs main
./generate_site.sh
git add docs
git commit --amend --no-edit
git push --force
git checkout main
```

