package com.guaridaludica.guaridaimpresionante;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.Base64;

public class GenerarVapidKeys {

    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();

        ECPublicKey pub = (ECPublicKey) kp.getPublic();
        ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();

        byte[] uncompressedPub = toUncompressedPoint(pub.getW());
        byte[] rawPriv = toFixed32Bytes(priv.getS());

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        System.out.println("VAPID_PUBLIC_KEY=" + encoder.encodeToString(uncompressedPub));
        System.out.println("VAPID_PRIVATE_KEY=" + encoder.encodeToString(rawPriv));
    }

    private static byte[] toUncompressedPoint(ECPoint w) {
        byte[] x = toFixed32Bytes(w.getAffineX());
        byte[] y = toFixed32Bytes(w.getAffineY());
        byte[] out = new byte[65];
        out[0] = 0x04;
        System.arraycopy(x, 0, out, 1, 32);
        System.arraycopy(y, 0, out, 33, 32);
        return out;
    }

    private static byte[] toFixed32Bytes(BigInteger value) {
        byte[] src = value.toByteArray();
        byte[] out = new byte[32];
        if (src.length == 32) {
            return src;
        }
        if (src.length > 32) {
            System.arraycopy(src, src.length - 32, out, 0, 32);
        } else {
            System.arraycopy(src, 0, out, 32 - src.length, src.length);
        }
        return out;
    }
}