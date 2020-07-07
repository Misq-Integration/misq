package bisq.core.trade.protocol;

import bisq.common.handlers.ErrorMessageHandler;
import bisq.core.trade.ArbitratorTrade;
import bisq.core.trade.messages.PrepareMultisigRequest;
import bisq.core.trade.protocol.tasks.ApplyFilter;
import bisq.core.trade.protocol.tasks.mediation.ArbitratorProcessesPrepareMultisigRequest;
import bisq.core.util.Validator;
import bisq.network.p2p.NodeAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ArbitratorProtocol extends TradeProtocol {
  
  private final ArbitratorTrade arbitratorTrade;
  
  public ArbitratorProtocol(ArbitratorTrade trade) {
    super(trade);
    
    this.arbitratorTrade = trade;

//    Trade.Phase phase = trade.getState().getPhase();
//    if (phase == Trade.Phase.TAKER_FEE_PUBLISHED) {
//        TradeTaskRunner taskRunner = new TradeTaskRunner(trade,
//                () -> handleTaskRunnerSuccess("BuyerSetupDepositTxListener"),
//                this::handleTaskRunnerFault);
//
//        taskRunner.addTasks(BuyerSetupDepositTxListener.class);
//        taskRunner.run();
//    } else if (trade.isFiatSent() && !trade.isPayoutPublished()) {
//        TradeTaskRunner taskRunner = new TradeTaskRunner(trade,
//                () -> handleTaskRunnerSuccess("BuyerSetupPayoutTxListener"),
//                this::handleTaskRunnerFault);
//
//        taskRunner.addTasks(BuyerSetupPayoutTxListener.class);
//        taskRunner.run();
//    }
  }
  
  ///////////////////////////////////////////////////////////////////////////////////////////
  // Start trade
  ///////////////////////////////////////////////////////////////////////////////////////////
  
  public void handlePrepareMultisigRequest(PrepareMultisigRequest tradeMessage,
                                     NodeAddress peerNodeAddress,
                                     ErrorMessageHandler errorMessageHandler) {
      Validator.checkTradeId(processModel.getOfferId(), tradeMessage);
      processModel.setTradeMessage(tradeMessage);
      processModel.setTempTradingPeerNodeAddress(peerNodeAddress);
      
      System.out.println("ARBITRATOR RECEIVED PREPARE MULTISIG REQUEST");

      TradeTaskRunner taskRunner = new TradeTaskRunner(arbitratorTrade,
              () -> handleTaskRunnerSuccess(tradeMessage, "handlePrepareMultisigRequest"),
              errorMessage -> {
                  errorMessageHandler.handleErrorMessage(errorMessage);
                  handleTaskRunnerFault(errorMessage);
              });
      taskRunner.addTasks(
              ArbitratorProcessesPrepareMultisigRequest.class,
              ApplyFilter.class
//              ArbitratorVerifyTraderAccounts.class,
//              VerifyPeersAccountAgeWitness.class,
//              MakerVerifyTakerFeePayment.class,
//              MakerSendsPrepareMultisigRequestToArbitrator.class
              //MakerSetsLockTime.class,
              //MakerCreateAndSignContract.class,
              //BuyerAsMakerCreatesAndSignsDepositTx.class,
              //BuyerSetupDepositTxListener.class,
              //BuyerAsMakerSendsInputsForDepositTxResponse.class
      );
      // We don't use a timeout here because if the DepositTxPublishedMessage does not arrive we
      // get the deposit tx set at MakerSetupDepositTxListener once it is seen in the bitcoin network
      taskRunner.run();
  }
}
