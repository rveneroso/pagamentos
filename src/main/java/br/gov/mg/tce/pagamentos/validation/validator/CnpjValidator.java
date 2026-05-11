package br.gov.mg.tce.pagamentos.validation.validator;

public class CnpjValidator {

    public static boolean isValid(String cnpj) {
        if (cnpj == null || !cnpj.matches("\\d{14}")) return false;

        if (cnpj.chars().distinct().count() == 1) return false;

        int[] nums = cnpj.chars().map(c -> c - '0').toArray();

        return calcDigito(nums, 12) == nums[12]
                && calcDigito(nums, 13) == nums[13];
    }

    private static int calcDigito(int[] nums, int length) {
        int[] pesos = (length == 12)
                ? new int[]{5,4,3,2,9,8,7,6,5,4,3,2}
                : new int[]{6,5,4,3,2,9,8,7,6,5,4,3,2};

        int soma = 0;

        for (int i = 0; i < length; i++) {
            soma += nums[i] * pesos[i];
        }

        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}
