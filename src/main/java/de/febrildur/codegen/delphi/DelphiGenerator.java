package de.febrildur.codegen.delphi;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenOperation;
import org.openapitools.codegen.CodegenParameter;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.CodegenResponse;
import org.openapitools.codegen.CodegenType;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.SupportingFile;
import org.openapitools.codegen.model.ModelMap;
import org.openapitools.codegen.model.OperationsMap;

public class DelphiGenerator extends DefaultCodegen implements CodegenConfig {

	protected String apiVersion = "1.0.0";

    private static final Map<String, String> delphiTypeMapping = Map.of(
            "Object", "TObject",
            "BigDecimal", "Double",
            "Long", "Int64"
    );

    /**
	 * Configures the type of generator.
	 * 
	 * @return the CodegenType for this generator
	 * @see org.openapitools.codegen.CodegenType
	 */
	public CodegenType getTag() {
		return CodegenType.OTHER;
	}

	/**
	 * Configures a friendly name for the generator. This will be used by the
	 * generator to select the library with the -g flag.
	 * 
	 * @return the friendly name for the generator
	 */
	public String getName() {
		return "delphi";
	}

	/**
	 * Provides an opportunity to inspect and modify operation data before the code
	 * is generated.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public OperationsMap postProcessOperationsWithModels(OperationsMap objs, List<ModelMap> allModels) {
		OperationsMap results = super.postProcessOperationsWithModels(objs, allModels);

		Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
		List<CodegenOperation> operation = (List<CodegenOperation>) operations.get("operation");

		for (CodegenOperation op : operation) {
			generateToString(op.allParams);
			generateToString(op.queryParams);
			generateToString(op.bodyParams);
			generateToString(op.pathParams);
			
			op.vendorExtensions.put("delphi-function", false);
			op.vendorExtensions.put("delphi-procedure", false);
			
			for (CodegenResponse resp : op.responses) {
				resp.dataType = getReservedWord(resp.dataType);

				if (resp.code.startsWith("2") && resp.dataType != null) {
					op.vendorExtensions.put("delphi-function", true);
					op.vendorExtensions.put("delphi-procedure", false);
					resp.vendorExtensions.put("delphi-relevant", true);
				}
			}
			if (!(boolean) op.vendorExtensions.get("delphi-function")) {
				op.vendorExtensions.put("delphi-procedure", true);
				op.responses.get(0).vendorExtensions.put("delphi-relevant", true);
			}
		}

		Set<String> arrays = new HashSet<>();
		for (ModelMap mod : allModels) {
			CodegenModel model = mod.getModel();

			model.setClassname(getReservedWord(model.getClassname()));
			for (CodegenProperty var : model.getVars()) {
				var.vendorExtensions.put("delphi-datatype", var.getDataType());
				if (var.isContainer) {
					String name = getReservedWord(var.getComplexType());
					var.vendorExtensions.put("delphi-datatype", "Array_of_" + name);
					arrays.add("Array_of_" + name + " = Array of " + name + ";");
				}
				var.vendorExtensions.put("delphi-datatype", getReservedWord((String) var.vendorExtensions.get("delphi-datatype")));
			}
		}
		additionalProperties.put("delphi-arrays", arrays);
		return results;
	}

	private void generateToString(List<CodegenParameter> params) {
		for (CodegenParameter param : params) {
			param.dataType = getReservedWord(param.dataType);
			if (!"String".equals(param.dataType)) {
				param.vendorExtensions.put("delphi-toString", param.paramName + ".ToString");
			} else {
				param.vendorExtensions.put("delphi-toString", param.paramName);
			}
		}
	}
	
	private String getReservedWord(String dataType) {
		if (dataType != null) {
            if (delphiTypeMapping.containsKey(dataType)) {
                return delphiTypeMapping.get(dataType);
            }
			if (reservedWords.contains(dataType.toLowerCase())) {
				return escapeReservedWord(dataType);
			} else {
				return dataType;
			}
		} else {
			return dataType;
		}
	}
	/**
	 * Returns human-friendly help for the generator. Provide the consumer with help
	 * tips, parameters here
	 * 
	 * @return A string value for the help message
	 */
	public String getHelp() {
		return "Generates a delphi client library.";
	}

	public DelphiGenerator() {
		outputFolder = "generated-code/delphi";
		templateDir = "delphi";
		apiPackage = "Rest.Service.Ebay.Account";
		reservedWords = new HashSet<String>(Arrays.asList("and", "array", "as", "asm", "begin", "case", "class",
				"const", "constructor", "destructor", "dispinterface", "div", "do", "downto", "else", "end", "except",
				"exports", "file", "finalization", "finally", "for", "function", "goto", "if", "implementation", "in",
				"inherited", "initialization", "inline", "interface", "is", "label", "library", "mod", "nil", "not",
				"object", "of", "or", "out", "packed", "procedure", "program", "property", "raise", "record", "repeat",
				"resourcestring", "set", "shl", "shr"/*, "string"*/, "then", "threadvar", "to", "try", "type", "unit",
				"until", "uses", "var", "while", "with", "xor"));

		additionalProperties.put("apiVersion", apiVersion);
		supportingFiles.add(new SupportingFile("delphi.mustache", "", apiPackage + ".pas"));
	}

	/**
	 * Escapes a reserved word as defined in the `reservedWords` array. Handle
	 * escaping those terms here. This logic is only called if a variable matches
	 * the reserved words
	 * 
	 * @return the escaped term
	 */
	@Override
	public String escapeReservedWord(String name) {
		return name + '_';
	}

	/**
	 * Location to write model files. You can use the modelPackage() as defined when
	 * the class is instantiated
	 */
	public String modelFileFolder() {
		return outputFolder;
	}

	/**
	 * Location to write api files. You can use the apiPackage() as defined when the
	 * class is instantiated
	 */
	@Override
	public String apiFileFolder() {
		return outputFolder;
	}

	/**
	 * override with any special text escaping logic to handle unsafe characters so
	 * as to avoid code injection
	 *
	 * @param input String to be cleaned up
	 * @return string with unsafe characters removed or escaped
	 */
	@Override
	public String escapeUnsafeCharacters(String input) {
		return input;
	}

	/**
	 * Escape single and/or double quote to avoid code injection
	 *
	 * @param input String to be cleaned up
	 * @return string with quotation mark removed or escaped
	 */
	public String escapeQuotationMark(String input) {
		return input.replace("'", "''");
	}
}