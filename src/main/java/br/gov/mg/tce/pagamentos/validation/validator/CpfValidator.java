package br.gov.mg.tce.pagamentos.validation.validator;

public class CpfValidator {

    public static boolean isValid(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) return false;

        // Não aceita CPFs com todos números iguais
        if (cpf.chars().distinct().count() == 1) return false;

        int[] nums = cpf.chars().map(c -> c - '0').toArray();

        return calcDigito(nums, 9) == nums[9]
                && calcDigito(nums, 10) == nums[10];
    }

    private static int calcDigito(int[] nums, int length) {
        int peso = length + 1;
        int soma = 0;

        for (int i = 0; i < length; i++) {
            soma += nums[i] * peso--;
        }

        int resto = (soma * 10) % 11;
        return (resto == 10) ? 0 : resto;
    }
}