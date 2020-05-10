package de.febrildur.codegen.delphi;

import org.junit.Test;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

public class DelphiGeneratorTest {

	// https://developer.ebay.com/api-docs/sell/account/resources/methods
	@Test
	public void launchCodeGenerator() {
		final CodegenConfigurator configurator = new CodegenConfigurator().setGeneratorName("delphi")
				.setInputSpec("sell_account_v1_oas3.json").setVerbose(false).setOutputDir("out/delphi");

		final ClientOptInput clientOptInput = configurator.toClientOptInput();
		DefaultGenerator generator = new DefaultGenerator();
		generator.opts(clientOptInput).generate();
	}
}