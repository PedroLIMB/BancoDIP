package com.example.bancodip.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import com.example.bancodip.R;
import com.example.bancodip.controller.ControllerBancoDados;
import com.example.bancodip.controller.Util;
import com.example.bancodip.databinding.ActivityMainBinding;
import com.example.bancodip.model.ModelBancoDados;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ControllerBancoDados controllerBancoDados;
    private Util util;
    private static final int REQUEST_TRANSFERIR = 123;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        controllerBancoDados = new ControllerBancoDados(this);
        util = new Util();

        Intent intentTrans = new Intent(MainActivity.this, TransferirActivity.class);
        Intent intent = getIntent();

        String nome = intent.getStringExtra("nome");
        String email = intent.getStringExtra("email");

        intentTrans.putExtra("email_trans", email);

        banco();

        try {
            controllerBancoDados.open();

            Double saldoBanco = controllerBancoDados.getSaldoByTitular(email);
            Double chequeBanco = controllerBancoDados.getChequeByTitular(email);
            String saldoString = String.valueOf(saldoBanco);
            String chequeString = String.valueOf(chequeBanco);

            binding.userName.setText("Olá, " + util.primeiraLetraMaiscula(nome));
            binding.saldoConta.setText("R$ " + saldoString);
            binding.chequeEspecialConta.setText("R$" + chequeString);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            controllerBancoDados.close();
        }


        binding.btnDepositar.setOnClickListener(v -> {
            controllerBancoDados.open();

            String valorCliente = binding.hintUserValor.getText().toString();

            if(!valorCliente.isEmpty()){
                try {

                    Double cheque = controllerBancoDados.getChequeByTitular(email);
                    Double valorSaldo = controllerBancoDados.getSaldoByTitular(email);
                    Double CHEQUEESPECIAL = controllerBancoDados.getChequeDEFIByTitular(email);

                    Double valorClienteDou = Double.parseDouble(valorCliente);

                    Double novoSaldo = Double.parseDouble(valorCliente) + valorSaldo ;
                    Double novoCheque = cheque + Double.parseDouble(valorCliente);

                    controllerBancoDados.updateSaldo(email, novoSaldo);
                    binding.saldoConta.setText("R$ "+ String.valueOf(novoSaldo));


                    if(valorSaldo < 0 && !(valorClienteDou < 0)){
                        controllerBancoDados.updateCheque(email, novoCheque);
                        binding.chequeEspecialConta.setText("R$ "+ String.valueOf(novoCheque));
                    }
                    if(novoSaldo >= 0 && cheque < CHEQUEESPECIAL && !(valorClienteDou < 0)){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("BANCO DIP");
                        builder.setMessage("CHEQUE ESPECIAL PAGO");
                        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // nada aqui
                            }
                        });

                        AlertDialog alerta = builder.create();
                        alerta.show();

                        controllerBancoDados.updateCheque(email, CHEQUEESPECIAL);
                        binding.chequeEspecialConta.setText("R$ " + String.valueOf(CHEQUEESPECIAL));

                    }

                }catch (Exception e){
                    e.printStackTrace();
                } finally {
                    controllerBancoDados.close();
                    binding.hintUserValor.setText("");
                }
            }

        });

        binding.btnSacar.setOnClickListener(v -> {
            controllerBancoDados.open();

            String valorCliente = binding.hintUserValor.getText().toString();

            if (!valorCliente.isEmpty()) {
                try {
                    Double saldo = controllerBancoDados.getSaldoByTitular(email);
                    Double cheque = controllerBancoDados.getChequeByTitular(email);
                    Double CHEQUEESPECIAL = controllerBancoDados.getChequeDEFIByTitular(email);

                    Double valorSaque = Double.parseDouble(valorCliente);

                    Double novoSaldo = saldo - valorSaque;
                    Double novoCheque = cheque - valorSaque;

                    Double novoSaldoMais = saldo + valorSaque;

                    if (saldo > 0 && novoSaldo >= 0 && !(valorSaque < 0)) {
                        controllerBancoDados.updateSaldo(email, novoSaldo);
                        binding.saldoConta.setText("R$ " + String.valueOf(novoSaldo));
                    } else if (saldo <= 0 && novoSaldo >= -CHEQUEESPECIAL && !(valorSaque < 0)) {
                        controllerBancoDados.updateSaldo(email, novoSaldo);
                        binding.saldoConta.setText("R$ " + String.valueOf(novoSaldo));

                        controllerBancoDados.updateCheque(email, novoCheque);
                        binding.chequeEspecialConta.setText("R$ " + String.valueOf(novoCheque));

                    } else if (saldo <= -CHEQUEESPECIAL) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("BANCO DIP");
                        builder.setMessage("Sem cheque especial");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Nada aqui
                            }
                        });

                        AlertDialog alerta = builder.create();
                        alerta.show();

                        controllerBancoDados.updateSaldo(email, -CHEQUEESPECIAL);
                        binding.saldoConta.setText("R$ " + String.valueOf(-CHEQUEESPECIAL));

                        controllerBancoDados.updateCheque(email, 0);
                        binding.chequeEspecialConta.setText("R$ " + String.valueOf(0.00));
                    } else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("BANCO DIP");
                        builder.setMessage("Saldo insuficiente");
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Nada aqui
                            }
                        });

                        AlertDialog alerta = builder.create();
                        alerta.show();
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    controllerBancoDados.close();
                    binding.hintUserValor.setText("");
                }
            }
        });




        binding.btnTransferir.setOnClickListener(v -> {
            startActivity(intentTrans);
        });

    }

    private void banco() {
        File f = new File("/data/data/com.example.bancodip/databases/BancoDEPEPE.db");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            File dir = Environment.getExternalStorageDirectory();
            File outputFile = new File(dir, "db_dump.db");
            fos = new FileOutputStream(outputFile);
            fis = new FileInputStream(f);
            
            while (true) {
                int i = fis.read();
                if (i != -1) {
                    fos.write(i);
                } else {
                    break;
                }
            }
            fos.flush();
            Toast.makeText(this, "DB dump OK", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "DB dump ERROR", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        controllerBancoDados.open();
        Intent intent = getIntent();

        String email = intent.getStringExtra("email");
        Double saldo = controllerBancoDados.getSaldoByTitular(email);
        Double cheque = controllerBancoDados.getChequeByTitular(email);

        binding.saldoConta.setText("R$ " + String.valueOf(saldo));
        binding.chequeEspecialConta.setText("R$ " + String.valueOf(cheque));

    }



}