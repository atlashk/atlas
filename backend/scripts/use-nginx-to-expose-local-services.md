# Use Nginx to Expose Local Services

In this section, we will set up Nginx as a reverse proxy to forward requests to our API Gateway running in Minikube. 
This allows us to access our services through a single entry point and can help with load balancing and security.

First, install Nginx:

```bash
sudo apt update
sudo apt install nginx -y
```

Expose NodePort **30080** for API Gateway:

```bash
kubectl patch svc api-gateway -n atlas -p '{"spec":{"type":"NodePort","ports":[{"port":8080,"targetPort":8080,"nodePort":30080}]}}'
```

Update Nginx configuration to forward requests to the API Gateway:

```bash
sudo nano /etc/nginx/sites-available/default
```

Add the following configuration at the end of the file:

```nginx
server {
    listen 8080;
    server_name _;

    location / {
        proxy_pass http://<minikube_ip>:30080;

        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

To get the Minikube IP address, run:

```bash
minikube ip
```

Finally, reload Nginx to apply the configuration changes:

```bash
sudo systemctl reload nginx
```
