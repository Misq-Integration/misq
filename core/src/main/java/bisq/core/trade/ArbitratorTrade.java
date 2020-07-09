package bisq.core.trade;

import org.bitcoinj.core.Coin;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.common.storage.Storage;
import bisq.core.btc.wallet.XmrWalletService;
import bisq.core.offer.Offer;
import bisq.core.proto.CoreProtoResolver;
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
          NodeAddress arbitratorNodeAddress,
          Storage<? extends TradableList> storage,
          XmrWalletService xmrWalletService) {
    super(offer, tradeAmount, txFee, takerFee, tradePrice, takerNodeAddress, makerNodeAddress, arbitratorNodeAddress, storage, xmrWalletService);
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

  ///////////////////////////////////////////////////////////////////////////////////////////
  // PROTO BUFFER
  ///////////////////////////////////////////////////////////////////////////////////////////

  @Override
  public protobuf.Tradable toProtoMessage() {
      return protobuf.Tradable.newBuilder()
              .setArbitratorTrade(protobuf.ArbitratorTrade.newBuilder()
                      .setTrade((protobuf.Trade) super.toProtoMessage()))
              .build();
  }

  public static Tradable fromProto(protobuf.ArbitratorTrade arbitratorTradeProto,
                                   Storage<? extends TradableList> storage,
                                   XmrWalletService xmrWalletService,
                                   CoreProtoResolver coreProtoResolver) {
      protobuf.Trade proto = arbitratorTradeProto.getTrade();
      return fromProto(new SellerAsTakerTrade(
                      Offer.fromProto(proto.getOffer()),
                      Coin.valueOf(proto.getTradeAmountAsLong()),
                      Coin.valueOf(proto.getTxFeeAsLong()),
                      Coin.valueOf(proto.getTakerFeeAsLong()),
                      proto.getIsCurrencyForTakerFeeBtc(),
                      proto.getTradePrice(),
                      proto.hasTakerNodeAddress() ? NodeAddress.fromProto(proto.getTakerNodeAddress()) : null,
                      proto.hasMakerNodeAddress() ? NodeAddress.fromProto(proto.getMakerNodeAddress()) : null,
                      proto.hasTradingPeerNodeAddress() ? NodeAddress.fromProto(proto.getTradingPeerNodeAddress()) : null,
                      proto.hasArbitratorNodeAddress() ? NodeAddress.fromProto(proto.getArbitratorNodeAddress()) : null,
                      proto.hasMediatorNodeAddress() ? NodeAddress.fromProto(proto.getMediatorNodeAddress()) : null,
                      proto.hasRefundAgentNodeAddress() ? NodeAddress.fromProto(proto.getRefundAgentNodeAddress()) : null,
                      storage,
                      xmrWalletService),
              proto,
              coreProtoResolver);
  }
}
