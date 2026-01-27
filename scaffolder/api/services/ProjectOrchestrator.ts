
import { ProjectConfig, FileNode, EntityConfig } from '../models/types';
import { TemplateEngineService } from '../infrastructure/TemplateEngineService';
import { FileSystemService } from '../infrastructure/FileSystemService';

type TemplateContext = ProjectConfig & {
  basePackagePath: string;
  jwtUserEntityName?: string;
  jwtUsernameFieldName?: string;
  jwtPasswordFieldName?: string;
  jwtRoleFieldName?: string;
};

export class ProjectOrchestrator {
  private templateEngine: TemplateEngineService;

  constructor(templatesDir: string) {
    this.templateEngine = new TemplateEngineService(templatesDir);
  }

  public async generateProject(config: ProjectConfig): Promise<FileNode> {
    const fileSystem = new FileSystemService(config.projectName);
    const context = this.prepareContext(config);

    // 1. Generate Base Structure
    await this.generateBase(fileSystem, context);

    // 2. Generate Stack
    await this.generateStack(fileSystem, context, config);

    // 3. Generate Authentication
    await this.generateAuthentication(fileSystem, context);

    // 4. Generate Entities
    await this.generateEntities(fileSystem, context, config);

    return fileSystem.getTree();
  }

  private prepareContext(config: ProjectConfig): TemplateContext {
    const normalizeValue = (value: string) => value.trim().toLowerCase();
    const resolveEntity = (name: string) =>
      (config.entities ?? []).find((e) => normalizeValue(e.name) === normalizeValue(name));
    const resolveFieldName = (entity: EntityConfig, name: string) =>
      (entity.fields ?? []).find((f) => normalizeValue(f.name) === normalizeValue(name))?.name;

    const jwt = config.stack.authentication === 'jwt' ? config.jwt : undefined;
    const jwtUsernameEntity = jwt?.fieldMapping?.username?.entityName?.trim()
      ? resolveEntity(jwt.fieldMapping.username.entityName)
      : undefined;
    const jwtUserEntityName = jwtUsernameEntity?.name;

    const jwtUsernameFieldName =
      jwtUsernameEntity && jwt?.fieldMapping?.username?.fieldName
        ? resolveFieldName(jwtUsernameEntity, jwt.fieldMapping.username.fieldName)
        : undefined;
    const jwtPasswordFieldName =
      jwtUsernameEntity && jwt?.fieldMapping?.password?.fieldName
        ? resolveFieldName(jwtUsernameEntity, jwt.fieldMapping.password.fieldName)
        : undefined;
    const jwtRoleFieldName =
      jwtUsernameEntity && jwt?.fieldMapping?.role?.fieldName
        ? resolveFieldName(jwtUsernameEntity, jwt.fieldMapping.role.fieldName)
        : undefined;

    return {
      ...config,
      basePackagePath: config.basePackage.replace(/\./g, '/'),
      jwtUserEntityName,
      jwtUsernameFieldName,
      jwtPasswordFieldName,
      jwtRoleFieldName,
    };
  }

  private async generateBase(fs: FileSystemService, context: TemplateContext) {
    const buildGradleContent = await this.templateEngine.render('spring-boot/base/build.gradle.hbs', context);
    fs.addFile(`build.gradle`, buildGradleContent);

    const settingsGradleContent = await this.templateEngine.render('spring-boot/base/settings.gradle.hbs', context);
    fs.addFile(`settings.gradle`, settingsGradleContent);

    const gitignoreContent = await this.templateEngine.render('spring-boot/base/.gitignore.hbs', context);
    fs.addFile(`.gitignore`, gitignoreContent);

    const gitattributesContent = await this.templateEngine.render('spring-boot/base/.gitattributes.hbs', context);
    fs.addFile(`.gitattributes`, gitattributesContent);

    // Generate Application class
    const appClassContent = await this.templateEngine.render('spring-boot/base/src/main/java/Application.java.hbs', context);
    fs.addFile(`src/main/java/${context.basePackagePath}/Application.java`, appClassContent);
    
    // Generate application.yml
    const appYamlContent = await this.templateEngine.render('spring-boot/base/src/main/resources/application.yml.hbs', context);
    fs.addFile(`src/main/resources/application.yml`, appYamlContent);

    // Generate README.md
    const readmeContent = await this.templateEngine.render('spring-boot/base/README.md.hbs', context);
    fs.addFile(`README.md`, readmeContent);
  }

  private async generateStack(fs: FileSystemService, context: TemplateContext, config: ProjectConfig) {
      const hasDockerCompose = config.stack.deployment.includes('docker-compose');
      const hasKubernetes = config.stack.deployment.includes('kubernetes');
      const hasHelm = config.stack.deployment.includes('helm');

      if (hasDockerCompose) {
          const dockerfileContent = await this.templateEngine.render('spring-boot/deployment/docker/Dockerfile.hbs', context);
          fs.addFile(`Dockerfile`, dockerfileContent);

          const composeContent = await this.templateEngine.render('spring-boot/deployment/docker-compose/docker-compose.yml.hbs', context);
          fs.addFile(`docker-compose.yml`, composeContent);
      }

      if (hasKubernetes && hasHelm) {
          const chartContent = await this.templateEngine.render('spring-boot/deployment/helm/Chart.yaml.hbs', context);
          fs.addFile(`helm/${context.projectName}/Chart.yaml`, chartContent);

          const valuesContent = await this.templateEngine.render('spring-boot/deployment/helm/values.yaml.hbs', context);
          fs.addFile(`helm/${context.projectName}/values.yaml`, valuesContent);

          const helmDeploymentContent = await this.templateEngine.render('spring-boot/deployment/helm/templates/deployment.yaml.hbs', context);
          fs.addFile(`helm/${context.projectName}/templates/deployment.yaml`, helmDeploymentContent);

          const helmServiceContent = await this.templateEngine.render('spring-boot/deployment/helm/templates/service.yaml.hbs', context);
          fs.addFile(`helm/${context.projectName}/templates/service.yaml`, helmServiceContent);

          return;
      }

      if (hasKubernetes) {
          const deploymentContent = await this.templateEngine.render('spring-boot/deployment/kubernetes/deployment.yaml.hbs', context);
          fs.addFile(`k8s/deployment.yaml`, deploymentContent);

          const serviceContent = await this.templateEngine.render('spring-boot/deployment/kubernetes/service.yaml.hbs', context);
          fs.addFile(`k8s/service.yaml`, serviceContent);
      }
  }

  private async generateAuthentication(fs: FileSystemService, context: TemplateContext) {
    if (context.stack.authentication !== 'jwt') return;

    const baseDir = `src/main/java/${context.basePackagePath}/security`;
    const templates = [
      { template: 'spring-boot/auth/jwt/src/main/java/security/SecurityConstant.java.hbs', out: `${baseDir}/SecurityConstant.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/JwtKeyProvider.java.hbs', out: `${baseDir}/JwtKeyProvider.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/JwtTokenService.java.hbs', out: `${baseDir}/JwtTokenService.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/JwtPrincipal.java.hbs', out: `${baseDir}/JwtPrincipal.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/JwtAuthenticationFilter.java.hbs', out: `${baseDir}/JwtAuthenticationFilter.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/SecurityConfig.java.hbs', out: `${baseDir}/SecurityConfig.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/CustomAuthenticationEntryPoint.java.hbs', out: `${baseDir}/CustomAuthenticationEntryPoint.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/CustomAccessDeniedHandler.java.hbs', out: `${baseDir}/CustomAccessDeniedHandler.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/AuthExceptionHandler.java.hbs', out: `${baseDir}/AuthExceptionHandler.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/CookieService.java.hbs', out: `${baseDir}/CookieService.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/UserDetailsImpl.java.hbs', out: `${baseDir}/UserDetailsImpl.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/UserDetailsServiceImpl.java.hbs', out: `${baseDir}/UserDetailsServiceImpl.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/AuthController.java.hbs', out: `${baseDir}/AuthController.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/dto/LoginRequest.java.hbs', out: `${baseDir}/dto/LoginRequest.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/dto/RefreshRequest.java.hbs', out: `${baseDir}/dto/RefreshRequest.java` },
      { template: 'spring-boot/auth/jwt/src/main/java/security/dto/TokenResponse.java.hbs', out: `${baseDir}/dto/TokenResponse.java` },
    ];

    for (const item of templates) {
      const content = await this.templateEngine.render(item.template, context);
      fs.addFile(item.out, content);
    }
  }

  private async generateEntities(fs: FileSystemService, context: TemplateContext, config: ProjectConfig) {
      if (!config.entities || config.entities.length === 0) return;

      for (const entity of config.entities) {
          const entityContext: TemplateContext & { entity: EntityConfig } = { ...context, entity };
          
          // Generate Entity Class
          const entityContent = await this.templateEngine.render('spring-boot/base/src/main/java/entity/Entity.java.hbs', entityContext);
          fs.addFile(`src/main/java/${context.basePackagePath}/entity/${entity.name}.java`, entityContent);

          // Generate Repository Interface
          const repoContent = await this.templateEngine.render('spring-boot/base/src/main/java/repository/Repository.java.hbs', entityContext);
          fs.addFile(`src/main/java/${context.basePackagePath}/repository/${entity.name}Repository.java`, repoContent);

          // Generate Service
          const serviceContent = await this.templateEngine.render('spring-boot/base/src/main/java/service/Service.java.hbs', entityContext);
          fs.addFile(`src/main/java/${context.basePackagePath}/service/${entity.name}Service.java`, serviceContent);
          
          // Generate Controller
          const controllerContent = await this.templateEngine.render('spring-boot/base/src/main/java/controller/Controller.java.hbs', entityContext);
          fs.addFile(`src/main/java/${context.basePackagePath}/controller/${entity.name}Controller.java`, controllerContent);
      }
  }
}
