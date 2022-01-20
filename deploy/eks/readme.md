#Instruction on how to run webhookie in EKS

##Prerequisites:

- Install awscli
- Install eksctl
- Install helm

###Make sure you follow awscli instuctions to setup yours



##1. Create EKS Cluster:
```
eksctl create cluster --name YOUR-PRODUCT-NAME --region AWS-REGION --fargate
```

###Note: This takes a couple of minutes and once it’s done, your kubectl is also ready and points to your cluster



##2. Prepare Helm charts
```
helm repo add webhookie-repo https://webhookie.github.io/helm-charts/
```


##3. Install webhookie:
```
helm install webhookie-all webhookie-repo/webhookie-all
```

Now you can wait for the cluster to be ready.

To monitor the status you can run:
```
kubectl get pods
```

Once all pods are ready, you can use webhookie.

###Note: for your test you can forward all required ports to your local host:

```
kubectl port-forward service/webhookie-all 4200:4200
kubectl port-forward service/webhookie-all 8080:8080
kubectl port-forward service/webhookie-all-keycloak 8800:8800
kubectl port-forward service/webhookie-all-keycloak 9900:9900
```

#Cleanup:
```
helm uninstall webhookie-all
eksctl delete cluster --region=AWS-REGION --name=YOUR-PRODUCT-NAME
```

You can also remove helm repository from your local
```
helm repo remove webhookie-repo
```

