# f16-project-smalleyt
Tiffany Smalley,
Christopher Paul King,
Adrian Buenavista,
Christopher Kirchner

## RUNNING MUTATION TESTS

### Testing Input Domain Partitioning:
within folder mutateIDP- run "mvn org.pitest:pitest-maven:mutationCoverage"
Results of previous run can be seen in 'mutateIDPResults.html'

### Testing Random Testing:
within folder mutateRandom- run "mvn org.pitest:pitest-maven:mutationCoverage"
Results of previous run can be seen in 'mutateRandomResults.html'

## REMAINING MVN TESTS (RANDOM and IDP)

within root folder - run 
"mvn clean"
"mvn compile test"
