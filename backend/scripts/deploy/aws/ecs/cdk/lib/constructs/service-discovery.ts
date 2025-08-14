import * as cdk from 'aws-cdk-lib';
import * as ec2 from 'aws-cdk-lib/aws-ec2';
import * as servicediscovery from 'aws-cdk-lib/aws-servicediscovery';
import { Construct } from 'constructs';

export interface ServiceDiscoveryProps {
  environmentName: string;
  vpc: ec2.Vpc;
  services?: string[];
}

export class ServiceDiscovery extends Construct {
  public readonly namespace: servicediscovery.PrivateDnsNamespace;
  public readonly services: Map<string, servicediscovery.Service>;

  constructor(scope: Construct, id: string, props: ServiceDiscoveryProps) {
    super(scope, id);

    const {
      environmentName,
      vpc,
      services = ['api-gateway', 'user-service', 'product-service', 'order-service', 'notification-service']
    } = props;

    // Create service discovery namespace
    this.namespace = new servicediscovery.PrivateDnsNamespace(this, 'ServiceDiscoveryNamespace', {
      name: `atlas.${environmentName}`,
      description: `Service discovery namespace for Atlas ${environmentName} environment`,
      vpc,
    });

    // Create service discovery services
    this.services = this.createServices(services);
  }

  private createServices(serviceNames: string[]): Map<string, servicediscovery.Service> {
    const services = new Map<string, servicediscovery.Service>();

    serviceNames.forEach(serviceName => {
      const service = new servicediscovery.Service(this, `${this.toPascalCase(serviceName)}DiscoveryService`, {
        name: serviceName,
        description: `${this.toTitleCase(serviceName)} service discovery`,
        namespace: this.namespace,
        dnsRecordType: servicediscovery.DnsRecordType.A,
        dnsTtl: cdk.Duration.seconds(60),
      });

      services.set(serviceName, service);
    });

    return services;
  }

  private toPascalCase(str: string): string {
    return str
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join('');
  }

  private toTitleCase(str: string): string {
    return str
      .split('-')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  public getService(serviceName: string): servicediscovery.Service {
    const service = this.services.get(serviceName);
    if (!service) {
      throw new Error(`Service ${serviceName} not found in service discovery`);
    }
    return service;
  }
} 