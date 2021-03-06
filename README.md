<a href="http://data.food.gov.uk/codes/ui/assets/images/logo.png"><img src="http://data.food.gov.uk/codes/ui/assets/images/logo.png" alt="FSA Logo" border="0" /></a>

<a href="https://github.com/FoodStandardsAgency/fsa-rn/wiki/images/alpha-banner.png"><img src="https://github.com/FoodStandardsAgency/fsa-rn/wiki/images/alpha-banner.png"
alt="ALPHA" width="70" height="20" border="0" /></a>  This is code is under development – your [feedback](https://github.com/FoodStandardsAgency/fsa-rn/issues) will help us to improve it.

# Reference Number Generation tooling (fsa-rn)

## About FSA-RN
Tooling and specification to support the generation of Reference Numbers in particular for a Food Business Establishment Registration.

### Purpose
The specification provides a standard for the provision of reference numbers.

The specification is implemented with specific code that can be run as part of a digital service within one or more of those organisations. The reference numbers provided would be stored in data and provided as a reference number in a digital service at the point of creation.

See the [**Rational and overview**](https://github.com/FoodStandardsAgency/fsa-rn/wiki/Rational-and-overview) pages on the [wiki](https://github.com/FoodStandardsAgency/fsa-rn/wiki) for more detail.  

### Current development phase
This is in the **Alpha phase** of development.  We use the alpha phase to: build prototypes of our services, test with users and in this case third-party suppliers and to show services are technically possible.

- `Document Version 0.1` <BR/>
- `Specification Version 0.1` <BR/>

- The initial Java implementation ([java-rn](java-rn) - `Version 0.0.1`)
- Further implementations are expected following initial testing discussions
- A JavaScript implementation to support Reference Number verification is planned following initial testing phase

### Documentation and specification
See the [project Wiki](https://github.com/FoodStandardsAgency/fsa-rn/wiki) for the **specification** and other documentation such as context, rational and detail on the underlying methods.
Note the [*Implementation and alpha notes*](https://github.com/FoodStandardsAgency/fsa-rn/wiki/Implementation-and-alpha-notes) for further details on the considerations to be aware of and this includes **known areas that may change post Alpha**


## Getting started
- See the [specification documentation](https://github.com/FoodStandardsAgency/fsa-rn/wiki)
- choose the code implementation (currently java-rn only)
- In implementation of the specification and code there is a need to assure that only one generator can operate with the combination of parameters: `authority`, `instance` and `type` this is may mean that direct client side browser implementations of the generator are problematic (and a server side implementation is required)


## Feedback
We would like to understand and problems with the design or approach that you may have.  Please feedback to: [data@food.gov.uk](mailto:data@food.gov.uk) or add an [Issue](https://github.com/FoodStandardsAgency/fsa-rn/issues)
