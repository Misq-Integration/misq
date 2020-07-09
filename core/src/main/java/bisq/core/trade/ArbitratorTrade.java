package bisq.core.trade;

import org.bitcoinj.core.Coin;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.storage.Storage;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.offer.Offer;
import bisq.core.trade.messages.PrepareMultisigRequest;
import bisq.core.trade.protocol.ArbitratorProtocol;
import bisq.network.p2p.NodeAddress;
import lombok.extern.slf4j.Slf4j;

/**
 * Trade in the context of an arbitrator.
 */
@Slf4j
public class ArbitratorTrade extends Trade {
  
  public ArbitratorTrade(Offer offer,
          Coin tradeAmount,
          Coin txFee,
          Coin takerFee,
          long tradePrice,
          NodeAddress takerNodeAddress,
          NodeAddress makerNodeAddress,
          Storage<? extends TradableList> storage,
          XmrWalletService xmrWalletService) {
    super(offer, tradeAmount, txFee, takerFee, tradePrice, takerNodeAddress, makerNodeAddress, storage, xmrWalletService);
  }

  public void handlePrepareMultisigRequest(PrepareMultisigRequest message, NodeAddress taker, ErrorMessageHandler errorMessageHandler) {
    ((ArbitratorProtocol) tradeProtocol).handlePrepareMultisigRequest(message, taker, errorMessageHandler);
  }

  @Override
  protected void createTradeProtocol() {
      tradeProtocol = new ArbitratorProtocol(this);
  }

  @Override
  public Coin getPayoutAmount() {
    throw new RuntimeException("Arbitrator does not have a payout amount");
  }

}
