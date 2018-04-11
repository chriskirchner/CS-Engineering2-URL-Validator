# Testing the Apache URL Validator

<p align="center">
<a href="https://asciinema.org/a/175539">
<img width=100% src="Random.PNG">
</a>
</p>

## MUTATION TESTS

### Testing Input Domain Partitioning

```sh
cd ./mutateIDP
mvn org.pitest:pitest-maven:mutationCoverage
firefox mutateIDPResults.html
```

### Testing Random Testing

```sh
cd mutateRandom
mvn org.pitest:pitest-maven:mutationCoverage
mutateRandomResults.html
```

## Random and IDP Tests

```sh
mvn clean
mvn compile test
```

## Credits
- Tiffany Smalley,
- Christopher Paul King,
- Adrian Buenavista,
- Christopher Kirchner