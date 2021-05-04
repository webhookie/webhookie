#NOTES
###Product code is required in the configuration**

**Failure Strategy:**
* fail open (provide a warning message but maintain full functionality) or 
* fail closed (disable all functionality in the application until a connection has been reestablished)
* something specific to your application. We strongly recommend that you refrain from failing closed after less than two hours of metering failures.

As an example of failing partially open, you could continue to allow access to the software but not allow the buyer to modify the software settings. Or, a buyer could still access the software, but would not be able to create additional users. Your software is responsible for defining and enforcing this failure mode. Your software’s failure mode must be included when your AMI is submitted, and it cannot be changed later.


**Links**
* [Metering service](https://docs.aws.amazon.com/marketplace/latest/userguide/metering-service.html)
* [AWS Marketplace metering and entitlement API permissions](https://docs.aws.amazon.com/marketplace/latest/userguide/iam-user-policy-for-aws-marketplace-actions.html)
* [AWS Marketplace Metering Service](https://docs.aws.amazon.com/marketplacemetering/latest/APIReference/Welcome.html)
* [Java Metering Service Integration](https://docs.aws.amazon.com/marketplace/latest/userguide/java-integration-example-meterusage.html)
* [AWS java Github](https://github.com/aws/aws-sdk-java-v2/#using-the-sdk)
* [**Creating container products for AWS Marketplace using Amazon EKS and AWS Fargate**](https://aws.amazon.com/blogs/awsmarketplace/creating-container-products-for-aws-marketplace-using-amazon-eks-and-aws-fargate/)
* [Nginx proxy](https://gist.github.com/soheilhy/8b94347ff8336d971ad0)
