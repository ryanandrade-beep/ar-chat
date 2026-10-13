package br.com.archat.arquitetura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/** Garante a regra de dependência da arquitetura limpa: as camadas internas não conhecem as externas. */
@AnalyzeClasses(packages = "br.com.archat", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaLimpaTest {

    @ArchTest
    static final ArchRule camadas = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Domínio").definedBy("..dominio..")
            .layer("Aplicação").definedBy("..aplicacao..")
            .layer("Infraestrutura").definedBy("..infraestrutura..")
            .layer("Apresentação").definedBy("..apresentacao..")
            .whereLayer("Apresentação").mayNotBeAccessedByAnyLayer()
            .whereLayer("Infraestrutura").mayOnlyBeAccessedByLayers("Apresentação")
            .whereLayer("Aplicação").mayOnlyBeAccessedByLayers("Infraestrutura", "Apresentação")
            .whereLayer("Domínio").mayOnlyBeAccessedByLayers("Aplicação", "Infraestrutura", "Apresentação");

    @ArchTest
    static final ArchRule dominioSemFrameworks = noClasses().that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..");

    @ArchTest
    static final ArchRule aplicacaoSemFrameworks = noClasses().that().resideInAPackage("..aplicacao..")
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..");
}
