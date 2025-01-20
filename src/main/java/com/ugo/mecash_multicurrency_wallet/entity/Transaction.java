package com.ugo.mecash_multicurrency_wallet.entity;

import com.ugo.mecash_multicurrency_wallet.enums.Status;
import com.ugo.mecash_multicurrency_wallet.enums.Type;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "Transaction")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(name = "processor_reference", columnDefinition="varchar(100)")
    private String processor_reference;

    @Column(name = "send_currency",columnDefinition="varchar(10)")
    private String send_currency;

    @Column(name = "receiver_currency",columnDefinition="varchar(10)")
    private String receive_currency;

    @Column(name = "send_country_code",columnDefinition="varchar(10)")
    private String send_country_code;

    @Column(name = "dest_country_code",columnDefinition="varchar(10)")
    private String dest_country_code;

    @Column(name = "account",columnDefinition="varchar(50)")
    private Long account;

    @Enumerated(EnumType.STRING)
    private Status payment_status;

    @Column(name = "response_code", columnDefinition="varchar(5)")
    private int response_code;

    @Column(name = "response_message",columnDefinition="varchar(255)")
    private String response_message;

    @Column(name = "naration",columnDefinition="varchar(255)")
    private String narration;

    @Column(name="transaction_purpose", columnDefinition="varchar(255)")
    private String transaction_purpose;

    @Column(name = "send_amount", columnDefinition = "DECIMAL(18,2)")
    private BigDecimal send_amount;

    @Column(name="receive_amount", columnDefinition="DECIMAL(18,2)")
    private BigDecimal receive_amount;

    @Column(name="exchange_rate", columnDefinition="DECIMAL(18,2)")
    private BigDecimal exchange_rate;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(unique = true, nullable = false)
    private String transactionReference;

    @Column(name = "send_issuer_code", columnDefinition = "varchar(10)")
    private String send_issuer_code;

    @Column(name = "destination_issuer_code", columnDefinition = "varchar(10)")
    private String destination_issuer_code;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "destination_wallet", columnDefinition = "int(10)")
    private Long receive_wallet;

}

