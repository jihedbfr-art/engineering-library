/**
 * Implementation de l'algorithme de Luhn : validation de FORMAT uniquement (checksum modulo 10).
 * Ne garantit PAS qu'un numero de carte existe ou soit associe a un compte reel.
 */
public final class LuhnAlgorithm {

    private LuhnAlgorithm() {
    }

    /**
     * Applique l'algorithme de Luhn sur une chaine de chiffres.
     * Retourne false si la chaine est vide, contient des caracteres non numeriques,
     * ou ne verifie pas le checksum de Luhn.
     */
    public static boolean isValid(String number) {
        if (number == null || number.isEmpty()) {
            return false;
        }
        for (int i = 0; i < number.length(); i++) {
            if (!Character.isDigit(number.charAt(i))) {
                return false;
            }
        }

        int sum = 0;
        boolean doubleDigit = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = number.charAt(i) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }

        return sum % 10 == 0;
    }
}
