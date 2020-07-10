/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.trade.protocol.tasks.mediation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;
import java.util.UUID;

import bisq.common.app.Version;
import bisq.common.taskrunner.TaskRunner;
import bisq.core.trade.Trade;
import bisq.core.trade.messages.PrepareMultisigRequest;
import bisq.core.trade.messages.PrepareMultisigResponse;
import bisq.core.trade.protocol.tasks.TradeTask;
import bisq.core.util.Validator;
import bisq.network.p2p.SendDirectMessageListener;
import lombok.extern.slf4j.Slf4j;
import monero.daemon.model.MoneroNetworkType;
import monero.wallet.MoneroWalletJni;
import monero.wallet.model.MoneroWalletConfig;

@Slf4j
public class ArbitratorProcessesPrepareMultisigRequest extends TradeTask {
  
    @SuppressWarnings({"unused"})
    public ArbitratorProcessesPrepareMultisigRequest(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            System.out.println("ArbitratorProcessesPrepareMultisigRequest.run()");
            log.debug("current trade state " + trade.getState());
            PrepareMultisigRequest request = (PrepareMultisigRequest) processModel.getTradeMessage();
            Validator.checkTradeId(processModel.getOfferId(), request);
            checkNotNull(request);
            
            // update to the latest peer address of our peer if the message is correct
            trade.setTradingPeerNodeAddress(processModel.getTempTradingPeerNodeAddress());
            processModel.removeMailboxMessageAfterProcessing(trade);
            
            System.out.println("ARBITRATOR TRADE INFO");
            System.out.println("Trading peer node address: " + trade.getTradingPeerNodeAddress());
            System.out.println("Maker node address: " + trade.getMakerNodeAddress());
            System.out.println("Taker node adddress: " + trade.getTakerNodeAddress());
            System.out.println("Mediator node address: " + trade.getMediatorNodeAddress());
            System.out.println("Arbitrator node address: " + trade.getArbitratorNodeAddress());
            
            // set trader's prepared multisig hex
            if (trade.getTakerNodeAddress().equals(request.getSenderNodeAddress())) {
              trade.setTakerPreparedMultisigHex(request.getPreparedMultisigHex());
            } else if (trade.getMakerNodeAddress().equals(request.getSenderNodeAddress())) {
              trade.setMakerPreparedMultisigHex(request.getPreparedMultisigHex());
            } else {
              throw new RuntimeException("PrepareMultisigRequest not from taker or maker.  This should never happen.");
            }
            
            // prepare multisig wallet
            String preparedHex;
            if (trade.getArbitratorPreparedMultisigHex() == null) {
              
              // create wallet for multisig
              // TODO (woodser): not persisted, manage in common util, set path, server
              MoneroWalletJni multisigWallet = MoneroWalletJni.createWallet(new MoneroWalletConfig()
                      .setPassword("abctesting123")
                      .setNetworkType(MoneroNetworkType.STAGENET));
              
              // prepare multisig
              preparedHex = multisigWallet.prepareMultisig();
              trade.setArbitratorPreparedMultisigHex(preparedHex);
            } else {
              preparedHex = trade.getArbitratorPreparedMultisigHex();
            }
            
            trade.persist();
            
            // create response to prepare multisig request
            PrepareMultisigResponse message = new PrepareMultisigResponse(
                    processModel.getOffer().getId(),
                    processModel.getMyNodeAddress(),
                    preparedHex,
                    UUID.randomUUID().toString(),
                    Version.getP2PMessageVersion(),
                    new Date().getTime());
            
            log.info("Send {} with offerId {} and uid {} to peer {}",
                    message.getClass().getSimpleName(), message.getTradeId(),
                    message.getUid(), trade.getTradingPeerNodeAddress());
            
            // send response to trader
            processModel.getP2PService().sendEncryptedDirectMessage(
                    request.getSenderNodeAddress(),
                    request.getPubKeyRing(),
                    message,
                    new SendDirectMessageListener() {
                        @Override
                        public void onArrived() {
                            log.info("{} arrived at peer: offerId={}; uid={}", message.getClass().getSimpleName(), message.getTradeId(), message.getUid());
                            complete();
                        }
                        @Override
                        public void onFault(String errorMessage) {
                            log.error("Sending {} failed: uid={}; peer={}; error={}", message.getClass().getSimpleName(), message.getUid(), trade.getTradingPeerNodeAddress(), errorMessage);
                            appendToErrorMessage("Sending message failed: message=" + message + "\nerrorMessage=" + errorMessage);
                            failed();
                        }
                    }
            );
        } catch (Throwable t) {
            failed(t);
        }
    }
}
