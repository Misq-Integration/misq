package bisq.core.trade;

import org.bitcoinj.core.Coin;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.storage.Storage;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.offer.Offer;
import bisq.core.trade.messages.PrepareMultisigRequest;
import bisq.core.trade.protocol.ArbitratorProtocol;
import bisq.network.p2p.NodeAddress;

/**
 * Trade in the context of an arbitrator.
 */
public class ArbitratorTrade extends Trade {
  
  public ArbitratorTrade(Offer offer,
          Coin tradeAmount,
          Coin txFee,
          Coin takerFee,
          long tradePrice,
          NodeAddress makerNodeAddress,
          NodeAddress takerNodeAddress,
          Storage<? extends TradableList> storage,
          XmrWalletService xmrWalletService) {
    super(offer, tradeAmount, txFee, takerFee, tradePrice, makerNodeAddress, takerNodeAddress, storage, xmrWalletService);
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
