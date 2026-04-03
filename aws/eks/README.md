# AWS EKS Deployment

This folder provides a practical AWS deployment path for the Order Management Application.

## Target AWS Services

- Amazon EKS for Kubernetes orchestration
- Amazon ECR for container registry
- Amazon RDS for PostgreSQL databases
- AWS Load Balancer Controller for ingress

## Recommended rollout

1. Create an EKS cluster:

```bash
eksctl create cluster -f aws/eks/cluster-config.yaml
```

2. Create three PostgreSQL databases in Amazon RDS or Aurora PostgreSQL:

- `productdb`
- `inventorydb`
- `orderdb`

3. Create ECR repositories:

```bash
aws ecr create-repository --repository-name order-management/product-service
aws ecr create-repository --repository-name order-management/inventory-service
aws ecr create-repository --repository-name order-management/order-service
```

4. Publish images:

```bash
chmod +x aws/eks/ecr-publish.sh
AWS_ACCOUNT_ID=123456789012 AWS_REGION=ap-south-1 IMAGE_TAG=v1 ./aws/eks/ecr-publish.sh
```

5. Update the image references in `k8s/base/*.yaml` if you use a different AWS account, region, or image tag.

6. Prepare Kubernetes secrets using `k8s/base/secrets-template.yaml`, replacing placeholder RDS endpoints and passwords.

7. Deploy the application:

```bash
kubectl apply -f k8s/base/namespace.yaml
kubectl apply -f k8s/base/configmap.yaml
kubectl apply -f k8s/base/secrets-template.yaml
kubectl apply -f k8s/base/product-service.yaml
kubectl apply -f k8s/base/inventory-service.yaml
kubectl apply -f k8s/base/order-service.yaml
kubectl apply -f aws/eks/alb-ingress.yaml
```

## Notes

- The application uses Flyway, so each service initializes its own schema automatically on startup.
- `order-service` is the public API entry point in the provided ingress example.
- For production hardening, add autoscaling, network policies, TLS, external secrets, and observability.
