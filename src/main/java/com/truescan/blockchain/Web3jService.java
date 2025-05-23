package com.truescan.blockchain;

import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Service
public class Web3jService {

    private final Web3j web3;

    public Web3jService() {
        // Connects to Sepolia testnet using Infura
        this.web3 = Web3j.build(
                new HttpService("https://sepolia.infura.io/v3/e749827f832b46278476e9266b39ed84")
        );
    }

    public Web3j getWeb3j() {
        return web3;
    }
}
