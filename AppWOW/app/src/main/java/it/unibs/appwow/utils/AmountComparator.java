package it.unibs.appwow.utils;

import java.util.Comparator;

import it.unibs.appwow.models.Amount;

/**
 * Created by Alessandro on 28/06/2016.
 * ordina una lista di amount nel seguente modo:
 * - localuser
 * -amount NEGATIVI in ordine decrescente di valore assoluto
 * -amount POSITIVI in ordine decrescente di valore assoluto
 * -amount pari a 0;
 *
 * a parità di amount ordina in ordine alfabetico.
 *
 * in realtà ordina tutto al contrario, per questo nell'adapter serve fare un "reverse".
 *
 */
public class AmountComparator implements Comparator<Amount> {
    private int mLocalUserId;

    public AmountComparator (int localUserId){
        mLocalUserId = localUserId;
    }
    @Override
    public int compare(Amount lhs, Amount rhs) {
        int res = 0;
        if(lhs.getUserId() == mLocalUserId) {
            return 1;
        } else if(rhs.getUserId() == mLocalUserId) {
            return -1;
        }
        double lamount = lhs.getAmount();
        double ramount = rhs.getAmount();
        if(lamount == ramount){
            res = 0;
        } else if(lamount<0){
            if(ramount<0){
                res =  lamount<ramount? 1:-1;
            } else {
                res = 1;
            }
        } else if(lamount>0){
            if(ramount>0){
                res = lamount>ramount? 1:-1;
            } else if(ramount<0){
                res = -1;
            } else {
                res = 1;
            }
        } else  if (lamount == 0){
            res = -1;
        }

        if(res == 0){
            //ordino in ordine alfabetico
            res = -lhs.getFullName().compareToIgnoreCase(rhs.getFullName());
        }
        return res;
    }
}
